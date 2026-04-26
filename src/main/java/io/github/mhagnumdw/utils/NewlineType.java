package io.github.mhagnumdw.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported line ending types.
 */
@Getter
@RequiredArgsConstructor
public enum NewlineType {
    NONE(""),
    LF("\n"),
    CRLF("\r\n"),
    CR("\r");

    private final String value;

}
