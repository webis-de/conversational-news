package de.aitools.commons.uima.standard;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.xml.sax.SAXException;

import de.aitools.commons.io.deserializer.Deserializer.Factory;
import de.aitools.commons.io.deserializer.InputStreamDeserializer;

/**
 * A collection reader that recursively reads all CAS files in a directory (also
 * in ZIP and GZ archives). 
 * 
 * @author johannes.kiesel@uni-weimar.de
 *
 */
public class CasReader extends DeserializerReader<InputStream> {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------

  /**
   * Parameter that specifies the suffix of valid files.
   * <p>
   * Can be empty to read all files.
   * </p>
   */
  public static final String PROPERTY_FILE_SUFFIX = "fileSuffix";

  /**
   * Default value for {@link #PROPERTY_FILE_SUFFIX}.
   */
  public static final String DEFAULT_FILE_SUFFIX = "";
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  protected Factory<InputStream> getFactory(final Properties properties) {
    final String fileSuffix = properties.getProperty(
        PROPERTY_FILE_SUFFIX, DEFAULT_FILE_SUFFIX);
    return new InputStreamDeserializer.Factory(fileSuffix);
  }

  @Override
  protected void setCas(
      final CAS cas, final InputStream inputStream, final String name) {
    try {
      cas.reset();
      XmiCasDeserializer.deserialize(inputStream, cas);
      inputStream.close();
    } catch (final SAXException e) {
      throw new IllegalArgumentException(e);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
