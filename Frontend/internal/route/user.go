package route

import (
	"server/internal/handler"
	"server/internal/middleware"

	"github.com/gofiber/fiber/v2"
)

func UserRoutes(app *fiber.App, userHandler handler.UserHandler) {
	app.Post("/user/signup", userHandler.SignUp)
	app.Post("/user/login", userHandler.Login)
	userRoutes := app.Group("/user")
	userRoutes.Use(middleware.AuthMiddleware)
	{
		userRoutes.Get("/list", userHandler.GetUsers)
		userRoutes.Get("/:id", userHandler.GetUser)
		userRoutes.Put("/:id", userHandler.UpdateUser)
		userRoutes.Delete("/:id", userHandler.DeleteUser)
	}
}
