package handler

import (
	"errors"
	"server/internal/database"
	"server/internal/model"

	"github.com/gofiber/fiber/v2"
	"gorm.io/gorm"
)

type CategoryHandler struct {
	DBService database.Service
}

func NewCategoryHandler(dbService database.Service) *CategoryHandler {
	return &CategoryHandler{DBService: dbService}
}

func (h *CategoryHandler) CreateCategory(c *fiber.Ctx) error {
	var category model.Category
	if err := c.BodyParser(&category); err != nil {
		return c.Status(400).JSON(err.Error())
	}

	res := h.DBService.GetDB().Create(&category)
	if res.Error != nil {
		return c.Status(500).JSON(res.Error.Error())
	} else {
		return c.Status(200).JSON(category)
	}
}

func (h *CategoryHandler) GetCategories(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	categories := []model.Category{}
	db.Find(&categories)

	return c.Status(200).JSON(categories)
}

func (h *CategoryHandler) GetCategory(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")
	var category model.Category
	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	db.Model(&model.Category{}).Preload("Products").Find(&category, id)

	return c.Status(200).JSON(category)
}

func (h *CategoryHandler) UpdateCategory(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")

	var category model.Category

	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	err = findCategory(id, db, &category)
	if err != nil {
		return c.Status(400).JSON(err.Error())
	}

	var updateData model.Category
	if err := c.BodyParser(&updateData); err != nil {
		return c.Status(500).JSON(err.Error())
	}

	category.Name = updateData.Name

	db.Save(&category)

	return c.Status(200).JSON(category)
}

func (h *CategoryHandler) DeleteCategory(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")

	var category model.Category

	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	err = findCategory(id, db, &category)

	if err != nil {
		return c.Status(400).JSON(err.Error())
	}

	if err = db.Delete(&category).Error; err != nil {
		return c.Status(404).JSON(err.Error())
	}

	return c.Status(200).JSON("Successfully deleted Category")
}

func findCategory(id int, db *gorm.DB, category *model.Category) error {
	db.Find(&category, "id = ?", id)
	if category.ID == 0 {
		return errors.New("category does not exist")
	}
	return nil
}
