// SPDX-License-Identifier: MIT
package com.daimler.sechub.util;

public interface ErrorLog {

    default void error(String message) {
        error(message,null);
    }
    default void error(String message, Throwable t) {
        /* just do nothing */
    }

    default void warn(String s){
        /* just do nothing */
    }
}
