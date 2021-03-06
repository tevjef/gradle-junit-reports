/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.gradle.junit

import java.io.IOException
import java.io.InputStream
import java.io.Writer
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader

object XmlUtils {

  @Throws(IOException::class)
  @JvmStatic
  fun <T : ReportHandler<*>> parseXml(handler: T, report: InputStream): T {
    try {
      val xmlReader = SAXParserFactory.newInstance().newSAXParser().xmlReader
      xmlReader.contentHandler = handler
      xmlReader.parse(InputSource(report))
      return handler
    } catch (e: SAXException) {
      throw IOException(e)
    } catch (e: ParserConfigurationException) {
      throw IOException(e)
    }

  }

  @Throws(TransformerException::class)
  @JvmStatic
  fun write(writer: Writer, document: Document): Writer {
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty(OutputKeys.METHOD, "xml")
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
    transformer.transform(DOMSource(document), StreamResult(writer))
    return writer
  }
}
