/*
 * The MIT License (MIT)

Copyright (c) 2016 Eric

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */

package de.epiceric.shopchest.exceptions;

//import static org.white_sdev.white_validations.parameters.ParameterValidator.notNullValidation;

/**
 * 
 * @author <a href="mailto:obed.vazquez@gmail.com">Obed Vazquez</a>
 * @since Nov 4, 2020
 */
public class ShopChestException extends RuntimeException {

    public ShopChestException(String string, Exception e) {
	super(string, e);
    }

    public ShopChestException(String string) {
	super(string);
    }

    public String getCauses() {
	final String BREAKLINE = "\n";
	final String BREAKLINE_REGEX = "[[\\r\\n]\\r\\n]";

	String causes = "Cause: " + getMessage() + BREAKLINE;
	String causeMessage;
	String lastAt = "";
	for (Throwable cause = this.getCause(); cause != null; cause = cause.getCause()) {
	    causeMessage = cause.getMessage();
	    if (causeMessage != null && !causeMessage.isBlank()) {

		String[] causedBy = causeMessage.split(BREAKLINE_REGEX);

		causes += "Cause: " + causedBy[0] + BREAKLINE;
		//causes+="---"+cause.getLocalizedMessage();
		//lastAt=causedBy.length>1?causedBy[1]:"";

	    }
	}
	causes += lastAt;
	return causes;
    }

}