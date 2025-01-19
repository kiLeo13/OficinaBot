package routes

import (
	"encoding/base64"
	"github.com/labstack/echo/v4"
	"net/http"
	"oficina-img/internal/service"
)

func GetLevelCard(c echo.Context) error {
	var ld service.LevelDataDTO
	if err := c.Bind(&ld); err != nil {
		return c.JSON(http.StatusBadRequest, service.ErrorMalformedJSON)
	}

	img, err := service.GenerateLevelCard(&ld)
	if err != nil {
		return c.JSON(err.Status, err)
	}

	resp := map[string]interface{}{
		"card_image":      base64.StdEncoding.EncodeToString(img),
		"isBase64Encoded": true,
	}
	return c.JSON(http.StatusOK, &resp)
}
