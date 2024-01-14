package server

import (
	"server/internal/database"
	"server/internal/handler"

	"github.com/gofiber/fiber/v2"
)

type FiberServer struct {
	*fiber.App
	db              database.Service
	userHandler     *handler.UserHandler
	productHandler  *handler.ProductHandler
	categoryHandler *handler.CategoryHandler
}

func New() *FiberServer {
	db := database.New()
	server := &FiberServer{
		App:             fiber.New(),
		db:              db,
		userHandler:     handler.NewUserHandler(db),
		productHandler:  handler.NewProductHandler(db),
		categoryHandler: handler.NewCategoryHandler(db),
	}

	return server
}
