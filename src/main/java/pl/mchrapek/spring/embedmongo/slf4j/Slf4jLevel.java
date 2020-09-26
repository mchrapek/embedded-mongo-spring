/*
 * The MIT License
 *
 * Copyright 2020- Marek Chrapek <marek.chrapek@hotmail.com>
 * Copyright 2013-2014 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.mchrapek.spring.embedmongo.slf4j;

import org.slf4j.Logger;

/**
 * Copied from https://gist.github.com/adutra/2911479.
 *
 * @author Alexandre Dutra
 */
public enum Slf4jLevel {

    TRACE {
        public void log(Logger logger, String message) {
            logger.trace(message);
        }
    },
    DEBUG {
        public void log(Logger logger, String message) {
            logger.debug(message);
        }
    },
    INFO {
        public void log(Logger logger, String message) {
            logger.info(message);
        }
    },
    WARN {
        public void log(Logger logger, String message) {
            logger.warn(message);
        }
    },
    ERROR {
        public void log(Logger logger, String message) {
            logger.error(message);
        }
    };

    public abstract void log(Logger logger, String message);
}
