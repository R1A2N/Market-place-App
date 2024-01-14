package server

import "server/internal/route"

func (s *FiberServer) RegisterFiberRoutes() {
	route.UserRoutes(s.App, *s.userHandler)
	route.ProductRoutes(s.App, *s.productHandler)
	route.CategoryRoutes(s.App, *s.categoryHandler)
}
