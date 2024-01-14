package route

import (
	"server/internal/handler"

	"github.com/gofiber/fiber/v2"
)

func CategoryRoutes(app *fiber.App, categoryHandler handler.CategoryHandler) {
	categoryRoutes := app.Group("/category")
	// categoryRoutes.Use(middleware.AuthMiddleware)
	// {
	categoryRoutes.Post("/create", categoryHandler.CreateCategory)
	categoryRoutes.Get("/list", categoryHandler.GetCategories)
	categoryRoutes.Get("/:id", categoryHandler.GetCategory)
	categoryRoutes.Put("/:id", categoryHandler.UpdateCategory)
	categoryRoutes.Delete("/:id", categoryHandler.DeleteCategory)
	// }
}
