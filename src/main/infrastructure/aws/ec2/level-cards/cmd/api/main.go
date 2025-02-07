package main

import (
	"github.com/labstack/echo/v4"
	"github.com/playwright-community/playwright-go"
	"oficina-img/internal/routes"
	"oficina-img/internal/service"
)

func main() {
	err := playwright.Install()
	if err != nil {
		panic(err)
	}

	pw, err := playwright.Run()
	if err != nil {
		panic(err)
	}
	service.InitializePlaywrightService(pw)

	e := echo.New()
	e.Static("/static", "./static")

	e.POST("/api/levels/cards", routes.GetLevelCard)
	e.POST("/api/levels/roles", routes.GetLevelsRoles)

	if err := e.Start(":8080"); err != nil {
		e.Logger.Fatal(err)
	}
}
