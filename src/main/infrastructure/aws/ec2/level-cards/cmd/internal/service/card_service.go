package service

import (
	"fmt"
	"github.com/playwright-community/playwright-go"
	"os"
	"path/filepath"
	"strings"
)

const (
	viewportWidth  = 934
	viewportHeight = 282
)

var (
	htmlFileURL = "file://" + filepath.ToSlash("./static/templates/template.html")
	pw          *playwright.Playwright
)

func SetPlaywright(playwright *playwright.Playwright) {
	pw = playwright
}

var OnlineColors = map[string]onlineColor{
	"ONLINE":  {35, 165, 90},
	"IDLE":    {240, 178, 50},
	"DND":     {242, 63, 67},
	"OFFLINE": {128, 128, 128},
}

type onlineColor struct {
	Red   int
	Green int
	Blue  int
}

func (o *onlineColor) getHtmlColor() string {
	return fmt.Sprintf("%d, %d, %d", o.Red, o.Green, o.Blue)
}

type LevelDataDTO struct {
	Username     string  `json:"username"`
	Rank         int     `json:"rank"`
	Level        int     `json:"level"`
	Xp           int     `json:"xp"`
	XpNext       int     `json:"xp_next"`
	AvatarUrl    string  `json:"avatar_url"`
	ProgressBar  float32 `json:"progress_bar"`
	OnlineStatus string  `json:"online_status"`
}

func GenerateLevelCard(ld *LevelDataDTO) ([]byte, *APIError) {
	if err := checkFields(ld); err != nil {
		return nil, err
	}

	color := OnlineColors[ld.OnlineStatus]
	html, err := getHTML(color, ld)
	if err != nil {
		return nil, err
	}

	browser, lerr := pw.Chromium.Launch(getLaunchOptions()...)
	if lerr != nil {
		fmt.Printf("Could not launcher chromium\n%s", lerr)
		return nil, ErrorInternalServer
	}

	page, perr := browser.NewPage()
	if perr != nil {
		fmt.Printf("Could not create page\n%s", perr)
		return nil, ErrorInternalServer
	}

	if err := page.SetContent(html, getPageLoadOptions()...); err != nil {
		fmt.Printf("Could not set page content\n%s", err)
		return nil, ErrorInternalServer
	}

	if err := page.SetViewportSize(viewportWidth, viewportHeight); err != nil {
		fmt.Printf("Could not set viewport size\n%s", err)
		return nil, ErrorInternalServer
	}

	img, serr := page.Screenshot(getScreenshotOptions()...)
	if serr != nil {
		fmt.Printf("Could not take screenshot\n%s", serr)
		return nil, ErrorInternalServer
	}
	return img, nil
}

func getHTML(statusColor onlineColor, ld *LevelDataDTO) (string, *APIError) {
	template, err := os.ReadFile("./static/templates/template.html")
	if err != nil {
		fmt.Printf("Could not read template file\n%s", err)
		return "", ErrorInternalServer
	}
	htmlColor := statusColor.getHtmlColor()

	html := string(template)
	html = strings.ReplaceAll(html, "'{{online.color}}'", fmt.Sprintf("rgb(%s)", htmlColor))
	html = strings.ReplaceAll(html, "'{{online.shadow}}'", fmt.Sprintf("rgba(%s, 0.5)", htmlColor))
	html = strings.ReplaceAll(html, "'{{progress.bar.percent}}'", fmt.Sprintf("%.2f%%", ld.ProgressBar))
	html = strings.ReplaceAll(html, "{{username}}", ld.Username)
	html = strings.ReplaceAll(html, "{{avatar.url}}", ld.AvatarUrl)
	html = strings.ReplaceAll(html, "{{rank}}", fmt.Sprintf("%d", ld.Rank))
	html = strings.ReplaceAll(html, "{{level}}", fmt.Sprintf("%d", ld.Level))
	html = strings.ReplaceAll(html, "{{xp.now}}", HumanizeNumber(ld.Xp))
	html = strings.ReplaceAll(html, "{{xp.next}}", HumanizeNumber(ld.XpNext))
	return html, nil
}

func checkFields(ld *LevelDataDTO) *APIError {
	if strings.TrimSpace(ld.Username) == "" {
		return ErrorMissingFields("username")
	}

	if ld.ProgressBar < 0 || ld.ProgressBar > 100 {
		return ErrorInvalidValue("progress_bar", ld.ProgressBar, "0 - 100")
	}

	// Validating numbers
	if err := validateNumber(ld.Rank, "rank"); err != nil {
		return err
	}
	if err := validateNumber(ld.Level, "level"); err != nil {
		return err
	}
	if err := validateNumber(ld.Xp, "xp"); err != nil {
		return err
	}
	if err := validateNumber(ld.XpNext, "xp_next"); err != nil {
		return err
	}

	_, ok := OnlineColors[ld.OnlineStatus]
	if !ok {
		return ErrorInvalidValue("online_status", ld.OnlineStatus, getOnlineStatusKeys()...)
	}
	return nil
}

func validateNumber(value int, fieldName string) *APIError {
	if value < 0 {
		return ErrorValueMustBePositive(fieldName, value)
	}
	return nil
}

func getOnlineStatusKeys() []string {
	keys := make([]string, 0, len(OnlineColors))
	for key := range OnlineColors {
		keys = append(keys, key)
	}
	return keys
}

func getLaunchOptions() []playwright.BrowserTypeLaunchOptions {
	return []playwright.BrowserTypeLaunchOptions{
		{Args: []string{"--headless"}},
	}
}

func getPageLoadOptions() []playwright.PageSetContentOptions {
	return []playwright.PageSetContentOptions{
		{WaitUntil: playwright.WaitUntilStateLoad},
	}
}

func getScreenshotOptions() []playwright.PageScreenshotOptions {
	return []playwright.PageScreenshotOptions{
		{Type: playwright.ScreenshotTypePng},
	}
}
