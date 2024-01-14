package model

import "gorm.io/gorm"

type Product struct {
	gorm.Model
	Name        string  `json:"name"`
	Desc        string  `json:"desc"`
	Image       string  `json:"image"`
	Price       float64 `json:"price"`
	Phone       string  `json:"phone"`
	IsAvailable bool    `json:"is_available"`
	SellerID    uint    `json:"seller_id"`
	CategoryID  uint    `json:"category_id"`
}
