/*
 * The MIT License
 *
 * Copyright 2024 samueladebowale.
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
package com.cometbid.sample.it.test;

import com.cometbid.sample.it.test.config.CustomCredentialsConverter;
import com.cometbid.sample.it.test.config.PropertyConversion;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.unit.DataSize;

/**
 *
 * @author samueladebowale
 */
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = PropertyConversion.class)
@ContextConfiguration(classes = CustomCredentialsConverter.class)
@TestPropertySource("classpath:spring-conversion-test.properties")
public class SpringPropertiesConversionUnitTest {

    @Autowired
    private PropertyConversion propertyConversion;

    @Test
    void whenUsingSpringDefaultSizeConversion_thenDataSizeObjectIsSet() {
        assertEquals(DataSize.ofMegabytes(500), propertyConversion.getUploadSpeed());
        assertEquals(DataSize.ofGigabytes(10), propertyConversion.getDownloadSpeed());
    }

    @Test
    void whenUsingSpringDefaultDurationConversion_thenDurationObjectIsSet() {
        assertEquals(Duration.ofDays(1), propertyConversion.getBackupDay());
        assertEquals(Duration.ofHours(8), propertyConversion.getBackupHour());
    }

    @Test
    void whenRegisteringCustomCredentialsConverter_thenCredentialsAreParsed() {
        assertEquals("user", propertyConversion.getCredentials().getUsername());
        assertEquals("123", propertyConversion.getCredentials().getPassword());
    }
}
