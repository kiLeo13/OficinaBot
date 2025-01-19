package service

import "fmt"

const (
	Thousand = 1_000
	Million  = 1_000 * Thousand
	Billion  = 1_000 * Million
)

func HumanizeNumber(value int) string {
	num := float64(value)
	switch {
	case num >= Billion:
		return fmt.Sprintf("%.2fB", num/Billion)
	case num >= Million:
		return fmt.Sprintf("%.2fM", num/Million)
	case num >= Thousand:
		return fmt.Sprintf("%.2fK", num/Thousand)
	default:
		return fmt.Sprintf("%d", value)
	}
}
