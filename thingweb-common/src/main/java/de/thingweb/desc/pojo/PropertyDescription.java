/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Siemens AG and the thingweb community
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

package de.thingweb.desc.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Property")
@JsonIgnoreProperties(ignoreUnknown=true)
public class PropertyDescription extends InteractionDescription {

    @JsonProperty
    private Boolean writable;
    
    @JsonProperty("outputData")
    private String outputType;
    
    @JsonCreator
    public PropertyDescription(@JsonProperty("name") String name, @JsonProperty("writable") Boolean writable, @JsonProperty("outputData") String outputType) {
      this.name = name;
      this.writable = writable;
      this.outputType = outputType;
    }
    
    public String getOutputType() {
      return outputType;
    }
    
    public boolean isWritable() {
      return writable;
    }

}