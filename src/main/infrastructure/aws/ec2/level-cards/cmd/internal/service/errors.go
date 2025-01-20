package service

import (
	"fmt"
	"strings"
)

var (
	ErrorMalformedJSON     = NewError(400, "Malformed JSON body")
	ErrorInternalServer    = NewError(500, "Internal server error")
	ErrorXpGreaterThanNext = NewError(400, "Field 'xp' is greater than 'xp_next', which should be impossible")
)

type APIError struct {
	Status  int    `json:"status"`
	Message string `json:"message"`
}

func NewError(status int, msg string, args ...any) *APIError {
	if len(args) > 0 {
		msg = fmt.Sprintf(msg, args...)
	}
	return &APIError{Status: status, Message: msg}
}

func ErrorMissingFields(fields ...string) *APIError {
	return NewError(400, "Missing required fields: %s", strings.Join(fields, ", "))
}

func ErrorValueMustBePositive(fieldName string, value int) *APIError {
	return NewError(400, "Field '%s' should be positive, provided: %d", fieldName, value)
}

func ErrorInvalidValue(fieldName string, valueProvided any, validValues ...any) *APIError {
	return NewError(400, "Invalid value provided for '%s': '%v', allowed: (%s)",
		fieldName, valueProvided, humanizeFields(validValues))
}

func ErrorCannotBeZero(fieldName string, value any) *APIError {
	return NewError(400, "Field '%s' cannot be less than or equal to zero, provided: %v", fieldName, value)
}

func humanizeFields(fields ...any) string {
	var builder strings.Builder
	for i, f := range fields {
		if i > 0 {
			builder.WriteString(", ")
		}
		builder.WriteString(fmt.Sprintf("%v", f))
	}
	return builder.String()
}
