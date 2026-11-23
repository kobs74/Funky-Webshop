package com.keyscout24.util;

import java.io.Serializable;

/** One flash message, shown once on the next page. category is "success" or "error" (CSS class alert-&lt;category&gt;). */
public record FlashMessage(String category, String message) implements Serializable {
}
