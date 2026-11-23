package com.keyscout24.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Replacement for Flask's flash(). Messages land in the model attribute "flashMessages",
 * which general.html renders. With RedirectAttributes they survive the redirect.
 */
public final class Flash {

    public static final String ATTRIBUTE = "flashMessages";

    private Flash() {
    }

    public static void success(Model model, String message) {
        add(model, "success", message);
    }

    public static void error(Model model, String message) {
        add(model, "error", message);
    }

    @SuppressWarnings("unchecked")
    public static void add(Model model, String category, String message) {
        Map<String, Object> target = model instanceof RedirectAttributes redirect
                ? (Map<String, Object>) redirect.getFlashAttributes()
                : model.asMap();
        Object existing = target.get(ATTRIBUTE);
        List<FlashMessage> messages = existing instanceof List<?> list
                ? new ArrayList<>((List<FlashMessage>) list)
                : new ArrayList<>();
        messages.add(new FlashMessage(category, message));
        target.put(ATTRIBUTE, messages);
    }
}
