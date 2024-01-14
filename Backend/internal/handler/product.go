package handler

import (
	"errors"
	"fmt"
	"server/internal/database"
	"server/internal/model"

	"github.com/gofiber/fiber/v2"
	"gorm.io/gorm"
)

type ProductHandler struct {
	DBService database.Service
}

func NewProductHandler(dbService database.Service) *ProductHandler {
	return &ProductHandler{DBService: dbService}
}

func (h *ProductHandler) CreateProduct(c *fiber.Ctx) error {
	var product model.Product
	if err := c.BodyParser(&product); err != nil {
		return c.Status(400).JSON(err.Error())
	}

	res := h.DBService.GetDB().Create(&product)
	if res.Error != nil {
		return c.Status(500).JSON(res.Error.Error())
	} else {
		return c.Status(200).JSON(product)
	}
}

func (h *ProductHandler) GetProducts(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	products := []model.Product{}
	db.Find(&products)
	fmt.Println(products)

	return c.Status(200).JSON(products)
}

func (h *ProductHandler) GetAvailableProducts(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	availableProducts := []model.Product{}
	db.Find(&availableProducts, "is_available = ?", true)

	return c.Status(200).JSON(availableProducts)
}

func (h *ProductHandler) GetProduct(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")
	var product model.Product
	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	if err := findProduct(id, db, &product); err != nil {
		return c.Status(400).JSON(err.Error())
	}

	return c.Status(200).JSON(product)
}

func (h *ProductHandler) UpdateProduct(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")

	var product model.Product

	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	err = findProduct(id, db, &product)
	if err != nil {
		return c.Status(400).JSON(err.Error())
	}

	type UpdateProduct struct {
		Desc        string  `json:"desc"`
		Image       string  `json:"image"`
		Price       float64 `json:"price"`
		IsAvailable bool    `json:"is_available"`
	}

	var updateData UpdateProduct

	if err := c.BodyParser(&updateData); err != nil {
		return c.Status(500).JSON(err.Error())
	}

	product.Desc = updateData.Desc
	product.Image = updateData.Image
	product.Price = updateData.Price
	product.IsAvailable = updateData.IsAvailable

	db.Save(&product)

	return c.Status(200).JSON(product)
}

func (h *ProductHandler) ToggleProductAvailability(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")

	var product model.Product

	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	err = findProduct(id, db, &product)
	if err != nil {
		return c.Status(400).JSON(err.Error())
	}

	// Toggle the IsAvailable field
	product.IsAvailable = !product.IsAvailable

	db.Save(&product)

	return c.Status(200).JSON(product)
}

func (h *ProductHandler) DeleteProduct(c *fiber.Ctx) error {
	db := h.DBService.GetDB()
	id, err := c.ParamsInt("id")

	var product model.Product

	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	err = findProduct(id, db, &product)

	if err != nil {
		return c.Status(400).JSON(err.Error())
	}

	if err = db.Delete(&product).Error; err != nil {
		return c.Status(404).JSON(err.Error())
	}

	return c.Status(200).JSON("Successfully deleted Product")
}

func (h *ProductHandler) UploadImage(c *fiber.Ctx) error {
	id, err := c.ParamsInt("id")
	if err != nil {
		return c.Status(400).JSON("Please ensure that :id is an integer")
	}

	file, err := c.FormFile("image")
	if err != nil {
		return c.Status(500).JSON(err.Error())
	}

	var product model.Product
	err = findProduct(id, h.DBService.GetDB(), &product)
	if err != nil {
		return c.Status(400).JSON(err.Error())
	}

	// Save the uploaded image file
	filename := fmt.Sprintf("product_%d_%s", id, file.Filename)
	if err := c.SaveFile(file, "uploads/"+filename); err != nil {
		return c.Status(500).JSON(err.Error())
	}

	// Update the product's image field with the new filename
	product.Image = filename
	h.DBService.GetDB().Save(&product)

	return c.Status(200).JSON(product)
}

func findProduct(id int, db *gorm.DB, product *model.Product) error {
	db.Find(&product, "id = ?", id)
	if product.ID == 0 {
		return errors.New("product does not exist")
	}
	return nil
}

func (h *ProductHandler) GetProductImage(c *fiber.Ctx) error {
	name := c.Params("name")
	if name == "" {
		return c.Status(400).JSON("Please provide a product name")
	}

	// Serve the image file
	return c.SendFile("uploads/" + name)
}

// Get all products by categoryId ans sellerId
func (h *ProductHandler) GetProductsByCategoryAndSeller(c *fiber.Ctx) error {
	categoryId, err := c.ParamsInt("categoryId")
	if err != nil {
		return err
	}
	sellerId, err := c.ParamsInt("userId")
	if err != nil {
		return err
	}
	db := h.DBService.GetDB()
	products := []model.Product{}
	db.Find(&products, "category_id = ? AND seller_id = ?", categoryId, sellerId)
	return c.Status(200).JSON(products)
}

// Get product by categoryId
func (h *ProductHandler) GetProductsByCategory(c *fiber.Ctx) error {
	categoryId, err := c.ParamsInt("id")
	if err != nil {
		return err
	}
	db := h.DBService.GetDB()
	products := []model.Product{}
	db.Find(&products, "is_available = ? AND category_id = ?", true, categoryId)
	return c.Status(200).JSON(products)
}

// Ger product by sellerId
func (h *ProductHandler) GetProductsBySeller(c *fiber.Ctx) error {
	sellerId, err := c.ParamsInt("userId")
	if err != nil {
		return err
	}
	db := h.DBService.GetDB()
	products := []model.Product{}
	db.Find(&products, "is_available = ? AND seller_id = ?", true, sellerId)
	return c.Status(200).JSON(products)
}
