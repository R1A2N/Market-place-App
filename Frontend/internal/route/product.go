package route

import (
	"server/internal/handler"

	"github.com/gofiber/fiber/v2"
)

func ProductRoutes(app *fiber.App, productHandler handler.ProductHandler) {
	productRoutes := app.Group("/product")
	// productRoutes.Use(middleware.AuthMiddleware)
	// {
	productRoutes.Post("/create", productHandler.CreateProduct)
	productRoutes.Post(":id/image", productHandler.UploadImage)
	productRoutes.Get("/image/:name", productHandler.GetProductImage)
	productRoutes.Get("/list", productHandler.GetProducts)
	productRoutes.Get("/category/:id", productHandler.GetProductsByCategory)
	productRoutes.Get("/list/:categoryId/:userId", productHandler.GetProductsByCategoryAndSeller)
	productRoutes.Get("/user/:userId", productHandler.GetProductsBySeller)
	productRoutes.Get("/available", productHandler.GetAvailableProducts)
	productRoutes.Put("/:id/toggleavailability", productHandler.ToggleProductAvailability)
	productRoutes.Get("/:id", productHandler.GetProduct)
	productRoutes.Put("/:id", productHandler.UpdateProduct)
	productRoutes.Delete("/:id", productHandler.DeleteProduct)
	// }
}
