package handler

import (
	"errors"
	"server/internal/database"
	"server/internal/model"
	"time"

	"github.com/gofiber/fiber/v2"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

type UserHandler struct {
	DBService database.Service
}

func NewUserHandler(dbService database.Service) *UserHandler {
	return &UserHandler{DBService: dbService}
}

type User struct {
	ID       uint            `json:"ID"`
	Name     string          `json:"name"`
	Email    string          `json:"email" gorm:"unique"`
	Products []model.Product `json:"products"`
}

func CreateResponseUser(user model.User) User {
	return User{ID: user.ID, Name: user.Username, Email: user.Email, Products: user.Products}
}

func (h *UserHandler) SignUp(c *fiber.Ctx) error {
	var user model.User
	if err := c.BodyParser(&user); err != nil {
		return c.Status(400).JSON(err.Error())
	}
	password := []byte(user.Password)
	hashedPassword, err := bcrypt.GenerateFromPassword(password, bcrypt.DefaultCost)
	if err != nil {
		return c.Status(500).JSON(err.Error())
	}
	user.Password = string(hashedPassword)

	res := h.DBService.GetDB().Create(&user)
	if res.Error != nil {
		return c.Status(500).JSON(res.Error.Error())
	} else {
		responseUser := CreateResponseUser(user)
		return c.Status(200).JSON(responseUser)
	}
}

func (h *UserHandler) GetUsers(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	users := []model.User{}
	db.Find(&users)
	responseUsers := []User{}
	for _, user := range users {
		responseUser := CreateResponseUser(user)
		responseUsers = append(responseUsers, responseUser)
	}

	return c.Status(200).JSON(responseUsers)
}

func (h *UserHandler) Login(c *fiber.Ctx) error {
	var loginRequest struct {
		Email    string `json:"email"`
		Password string `json:"password"`
	}

	if err := c.BodyParser(&loginRequest); err != nil {
		return c.Status(400).JSON(fiber.Map{"error": "Invalid JSON payload"})
	}

	db := h.DBService.GetDB()
	var user model.User

	db.Find(&user, "email = ?", loginRequest.Email)

	if user.Email == "" {
		return c.Status(404).JSON(fiber.Map{"error": "User not found"})
	}

	bcryptErr := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(loginRequest.Password))
	if bcryptErr != nil {
		return c.Status(401).JSON(fiber.Map{"error": "Wrong Password"})
	}

	token, err := model.GenerateJWT("access", user, time.Minute*60*24)
	if err != nil {
		return c.Status(500).JSON(fiber.Map{"error": "Error generating token"})
	}

	return c.Status(200).JSON(fiber.Map{"token": token, "ID": user.ID, "is_buyer": user.IsBuyer})
}

func (h *UserHandler) GetUser(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")
	var user model.User
	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}
	db.Model(&model.User{}).Preload("Products").Find(&user, id)

	responseUser := CreateResponseUser(user)

	return c.Status(200).JSON(responseUser)
}

func (h *UserHandler) UpdateUser(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")

	var user model.User

	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	err = findUser(id, db, &user)
	if err != nil {
		return c.Status(400).JSON(err.Error())
	}

	type UpdateUser struct {
		Name     string `json:"name"`
		Password string `json:"password"`
	}

	var updateData UpdateUser

	if err := c.BodyParser(&updateData); err != nil {
		return c.Status(500).JSON(err.Error())
	}

	user.Username = updateData.Name
	user.Password = updateData.Password

	db.Save(&user)

	responseUser := CreateResponseUser(user)

	return c.Status(200).JSON(responseUser)
}

func (h *UserHandler) DeleteUser(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")

	var user model.User

	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	err = findUser(id, db, &user)

	if err != nil {
		return c.Status(400).JSON(err.Error())
	}

	if err = db.Delete(&user).Error; err != nil {
		return c.Status(404).JSON(err.Error())
	}

	return c.Status(200).JSON("Successfully deleted User")
}

func findUser(id int, db *gorm.DB, user *model.User) error {
	db.Find(&user, "id = ?", id)
	if user.ID == 0 {
		return errors.New("user does not exist")
	}
	return nil
}
