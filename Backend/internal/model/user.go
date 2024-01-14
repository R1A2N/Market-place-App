package model

import (
	"log"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v4"
	"github.com/joho/godotenv"
	"gorm.io/gorm"
)

type User struct {
	gorm.Model
	Username    string    `json:"name"`
	PhoneNumber string    `json:"phone_number"`
	Email       string    `json:"email" gorm:"unique"`
	Password    string    `json:"password"`
	IsBuyer     bool      `json:"is_buyer"`
	Products    []Product `gorm:"foreignKey:SellerID",json:"products"`
}

func GenerateJWT(token_type string, user User, expiry time.Duration) (string, error) {
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"id":    user.ID,
		"email": user.Email,
		"exp":   time.Now().Add(expiry).Unix(),
	})

	err := godotenv.Load()
	if err != nil {
		log.Fatal("Error loading .env file")
	}

	if token_type == "access" {
		// Generate encoded token and send it as response.
		access := os.Getenv("ACCESS_KEY")

		tokenString, err := token.SignedString([]byte(access))

		return tokenString, err
	}

	if token_type == "refresh" {
		// Generate encoded token and send it as response.
		refresh := os.Getenv("REFRESH_KEY")

		tokenString, err := token.SignedString([]byte(refresh))

		return tokenString, err
	}
	return "", nil
}
