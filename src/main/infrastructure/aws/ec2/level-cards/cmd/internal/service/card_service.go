package service

import (
	"fmt"
	"github.com/playwright-community/playwright-go"
	"math/rand/v2"
	"os"
	"strings"
)

var (
	viewport = &playwright.Size{Width: 934, Height: 282}
	pw       *playwright.Playwright
	chromium playwright.Browser
)

func InitializePlaywrightService(playwright *playwright.Playwright) {
	pw = playwright
	browser, err := pw.Chromium.Launch(getLaunchOptions())
	if err != nil {
		panic(err)
	}
	chromium = browser
}

var OnlineColors = map[string]*Color{
	"ONLINE":  {35, 165, 90},
	"IDLE":    {240, 178, 50},
	"DND":     {242, 63, 67},
	"OFFLINE": {128, 128, 128},
}

type LevelDataDTO struct {
	Username     string `json:"username"`
	AvatarUrl    string `json:"avatar_url"`
	Rank         int    `json:"rank"`
	Level        int    `json:"level"`
	Xp           int    `json:"xp"`
	XpNext       int    `json:"xp_next"`
	ThemeColor   int    `json:"theme_color"`
	OnlineStatus string `json:"online_status"`
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

	page, perr := chromium.NewPage(getPageOptions())
	if perr != nil {
		fmt.Printf("Could not create page\n%s", perr)
		return nil, ErrorInternalServer
	}
	defer page.Close()

	if err := page.SetContent(html, getPageLoadOptions()); err != nil {
		fmt.Printf("Could not set page content\n%s", err)
		return nil, ErrorInternalServer
	}

	img, serr := page.Screenshot(getScreenshotOptions())
	if serr != nil {
		fmt.Printf("Could not take screenshot\n%s", serr)
		return nil, ErrorInternalServer
	}
	return img, nil
}

func getHTML(statusColor *Color, ld *LevelDataDTO) (string, *APIError) {
	template, err := os.ReadFile("./static/templates/template.html")
	if err != nil {
		fmt.Printf("Could not read template file\n%s", err)
		return "", ErrorInternalServer
	}
	progress := float32(ld.Xp) * 100 / float32(ld.XpNext)
	themeColor := FromRGB(ld.ThemeColor).ToHtmlRGB()
	backImg := rand.IntN(9) + 1

	html := string(template)
	html = strings.ReplaceAll(html, "'{{online.color}}'", statusColor.ToHtmlRGB())
	html = strings.ReplaceAll(html, "'{{online.shadow}}'", statusColor.ToHtmlRGBA(0.5))
	html = strings.ReplaceAll(html, "'{{progress.bar.percent}}'", fmt.Sprintf("%.2f%%", progress))
	html = strings.ReplaceAll(html, "'{{theme.color}}'", themeColor)
	html = strings.ReplaceAll(html, "{{username}}", ld.Username)
	html = strings.ReplaceAll(html, "{{avatar.url}}", ld.AvatarUrl)
	html = strings.ReplaceAll(html, "{{rank}}", fmt.Sprintf("%d", ld.Rank))
	html = strings.ReplaceAll(html, "{{level}}", fmt.Sprintf("%d", ld.Level))
	html = strings.ReplaceAll(html, "{{background.image}}", fmt.Sprintf("background-%d.png", backImg))
	html = strings.ReplaceAll(html, "{{xp.now}}", HumanizeNumber(ld.Xp))
	html = strings.ReplaceAll(html, "{{xp.next}}", HumanizeNumber(ld.XpNext))
	return html, nil
}

func checkFields(ld *LevelDataDTO) *APIError {
	if strings.TrimSpace(ld.Username) == "" {
		return ErrorMissingFields("username")
	}

	if !IsColorValid(ld.ThemeColor) {
		return ErrorInvalidValue("theme_color", ld.ThemeColor, fmt.Sprintf("0 - %d", MaxColorValue))
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
	if err := validateNumber(ld.XpNext, "xp_next"); err != nil || ld.XpNext == 0 {
		return ErrorCannotBeZero("xp_next", ld.XpNext)
	}
	if ld.Xp > ld.XpNext {
		return ErrorXpGreaterThanNext
	}

	_, ok := OnlineColors[ld.OnlineStatus]
	if !ok {
		return ErrorInvalidValue("online_status", ld.OnlineStatus, getOnlineStatusKeys())
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

func getLaunchOptions() playwright.BrowserTypeLaunchOptions {
	return playwright.BrowserTypeLaunchOptions{
		Args: []string{"--headless"},
	}
}

func getPageOptions() playwright.BrowserNewPageOptions {
	return playwright.BrowserNewPageOptions{
		Viewport: viewport,
	}
}

func getPageLoadOptions() playwright.PageSetContentOptions {
	return playwright.PageSetContentOptions{
		WaitUntil: playwright.WaitUntilStateLoad,
	}
}

func getScreenshotOptions() playwright.PageScreenshotOptions {
	return playwright.PageScreenshotOptions{
		Type: playwright.ScreenshotTypePng,
	}
}
