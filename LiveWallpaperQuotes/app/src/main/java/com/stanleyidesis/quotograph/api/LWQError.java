package com.stanleyidesis.quotograph.api;

import com.stanleyidesis.quotograph.LWQApplication;

/**
 * Copyright (c) 2016 Stanley Idesis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * LWQError.java
 * @author Stanley Idesis
 *
 * From Quotograph
 * https://github.com/stanidesis/quotograph
 *
 * Please report any issues
 * https://github.com/stanidesis/quotograph/issues
 *
 * Date: 02/21/2016
 */
public class LWQError {

    public static LWQError create(String errorMessage) {
        LWQError lwqError = new LWQError(errorMessage);
        LWQApplication.getLogger().logError(lwqError);
        return lwqError;
    }

    public static LWQError create(Throwable errorThrowable) {
        LWQError lwqError = new LWQError(errorThrowable);
        LWQApplication.getLogger().logError(lwqError);
        return lwqError;
    }

    public static LWQError create(String errorMessage, Throwable errorThrowable) {
        LWQError lwqError = new LWQError(errorMessage, errorThrowable);
        LWQApplication.getLogger().logError(lwqError);
        return lwqError;
    }

    public static void log(String errorMessage) {
        LWQApplication.getLogger().logError(LWQError.create(errorMessage));
    }

    final String errorMessage;
    final Throwable errorThrowable;

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getErrorThrowable() {
        return errorThrowable;
    }

    private LWQError(String errorMessage) {
        this(errorMessage, new RuntimeException("Auto-generated"));
    }

    private LWQError(Throwable errorThrowable) {
        this(errorThrowable.getMessage(), errorThrowable);
    }

    private LWQError(String errorMessage, Throwable errorThrowable) {
        this.errorMessage = errorMessage;
        this.errorThrowable = errorThrowable;
    }
}
