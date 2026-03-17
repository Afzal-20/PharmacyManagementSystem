package com.my.pharmacy.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator — Centralized input validation for all controllers.
 *
 * Usage:
 *   List<String> errors = Validator.validate()
 *       .requireText(nameField, "Name")
 *       .requirePositiveInt(qtyField, "Quantity")
 *       .requirePositiveDouble(priceField, "Price")
 *       .getErrors();
 *
 *   if (!errors.isEmpty()) {
 *       NotificationService.error(String.join("\n", errors));
 *       return;
 *   }
 */
public class Validator {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);
    private final List<String> errors = new ArrayList<>();

    private Validator() {}

    public static Validator validate() {
        return new Validator();
    }

    // ── Text fields ────────────────────────────────────────────────────────────

    public Validator requireText(TextField field, String fieldName) {
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            String msg = fieldName + " is required.";
            log.debug("Validation failed: {}", msg);
            errors.add(msg);
            if (field != null) markInvalid(field);
        } else {
            markValid(field);
        }
        return this;
    }

    public Validator requirePositiveDouble(TextField field, String fieldName) {
        requireText(field, fieldName);
        if (!errors.stream().anyMatch(e -> e.startsWith(fieldName))) {
            try {
                double val = Double.parseDouble(field.getText().trim());
                if (val <= 0) {
                    String msg = fieldName + " must be greater than zero.";
                    log.debug("Validation failed: {}", msg);
                    errors.add(msg);
                    markInvalid(field);
                } else {
                    markValid(field);
                }
            } catch (NumberFormatException e) {
                String msg = fieldName + " must be a valid number.";
                log.debug("Validation failed: {}", msg);
                errors.add(msg);
                markInvalid(field);
            }
        }
        return this;
    }

    public Validator requirePositiveInt(TextField field, String fieldName) {
        requireText(field, fieldName);
        if (!errors.stream().anyMatch(e -> e.startsWith(fieldName))) {
            try {
                int val = Integer.parseInt(field.getText().trim());
                if (val <= 0) {
                    String msg = fieldName + " must be greater than zero.";
                    log.debug("Validation failed: {}", msg);
                    errors.add(msg);
                    markInvalid(field);
                } else {
                    markValid(field);
                }
            } catch (NumberFormatException e) {
                String msg = fieldName + " must be a whole number.";
                log.debug("Validation failed: {}", msg);
                errors.add(msg);
                markInvalid(field);
            }
        }
        return this;
    }

    public Validator requireNonNegativeDouble(TextField field, String fieldName) {
        requireText(field, fieldName);
        if (!errors.stream().anyMatch(e -> e.startsWith(fieldName))) {
            try {
                double val = Double.parseDouble(field.getText().trim());
                if (val < 0) {
                    String msg = fieldName + " cannot be negative.";
                    log.debug("Validation failed: {}", msg);
                    errors.add(msg);
                    markInvalid(field);
                } else {
                    markValid(field);
                }
            } catch (NumberFormatException e) {
                String msg = fieldName + " must be a valid number.";
                log.debug("Validation failed: {}", msg);
                errors.add(msg);
                markInvalid(field);
            }
        }
        return this;
    }

    public Validator requireMaxLength(TextField field, String fieldName, int maxLength) {
        if (field != null && field.getText() != null && field.getText().length() > maxLength) {
            String msg = fieldName + " must be " + maxLength + " characters or less.";
            log.debug("Validation failed: {}", msg);
            errors.add(msg);
            markInvalid(field);
        }
        return this;
    }

    public <T> Validator requireSelection(ComboBox<T> combo, String fieldName) {
        if (combo == null || combo.getValue() == null) {
            String msg = fieldName + " must be selected.";
            log.debug("Validation failed: {}", msg);
            errors.add(msg);
        }
        return this;
    }

    public Validator requirePasswordMatch(TextField pass, TextField confirm) {
        if (pass != null && confirm != null) {
            if (!pass.getText().equals(confirm.getText())) {
                String msg = "Passwords do not match.";
                log.debug("Validation failed: {}", msg);
                errors.add(msg);
                markInvalid(confirm);
            } else {
                markValid(confirm);
            }
        }
        return this;
    }

    public Validator requireMinLength(TextField field, String fieldName, int minLength) {
        if (field != null && (field.getText() == null || field.getText().trim().length() < minLength)) {
            String msg = fieldName + " must be at least " + minLength + " characters.";
            log.debug("Validation failed: {}", msg);
            errors.add(msg);
            markInvalid(field);
        }
        return this;
    }

    // ── Results ────────────────────────────────────────────────────────────────

    public List<String> getErrors() {
        return errors;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public String getErrorMessage() {
        return String.join("\n", errors);
    }

    // ── Visual feedback ────────────────────────────────────────────────────────

    private void markInvalid(TextField field) {
        field.getStyleClass().remove("field-valid");
        if (!field.getStyleClass().contains("field-invalid")) {
            field.getStyleClass().add("field-invalid");
        }
    }

    private void markValid(TextField field) {
        field.getStyleClass().remove("field-invalid");
    }
}