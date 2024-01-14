package database

import (
	"context"
	"fmt"
	"log"
	"os"
	"server/internal/model"
	"time"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

type Service interface {
	Health() map[string]string
	GetDB() *gorm.DB
}

type service struct {
	Db *gorm.DB
}

var (
	database = os.Getenv("DB_DATABASE")
	password = os.Getenv("DB_PASSWORD")
	username = os.Getenv("DB_USERNAME")
	port     = os.Getenv("DB_PORT")
	host     = os.Getenv("DB_HOST")
)

func New() Service {
	dsn := fmt.Sprintf("user=%s password=%s dbname=%s port=%s host=%s sslmode=disable", username, password, database, port, host)
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatal(err)
	}

	// Migrate your database schemas here if needed
	db.AutoMigrate(&model.User{}, &model.Category{}, &model.Product{})

	s := &service{Db: db}
	return s
}

func (s *service) GetDB() *gorm.DB {
	return s.Db
}

func (s *service) Health() map[string]string {
	ctx, cancel := context.WithTimeout(context.Background(), 1*time.Second)
	defer cancel()

	sqlDB, err := s.Db.DB()
	if err != nil {
		log.Fatalf(fmt.Sprintf("db down: %v", err))
	}

	err = sqlDB.PingContext(ctx)
	if err != nil {
		log.Fatalf(fmt.Sprintf("db down: %v", err))
	}

	return map[string]string{
		"message": "It's healthy",
	}
}
