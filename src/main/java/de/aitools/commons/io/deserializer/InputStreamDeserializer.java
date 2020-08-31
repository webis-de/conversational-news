package de.aitools.commons.io.deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Simple deserializer that just passes the input stream.
 *
 * @author johannes.kiesel@uni-weimar.de
 *
 */
public class InputStreamDeserializer
extends SingleElementDeserializer<InputStream> {
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------

  /**
   * Creates a new deserializer that just passes given stream.
   * @param name A name for the string
   * @param input The stream to deserialize from
   */
  public InputStreamDeserializer(final String name, final InputStream input) {
    super(name, input);
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  protected InputStream parse(final InputStream input) throws Exception {
    return input;
  }
  
  // -------------------------------------------------------------------------
  // FACTORY
  // -------------------------------------------------------------------------
  
  /**
   * A factory for {@link Deserializer}s that read from files.
   *
   * @author johannes.kiesel@uni-weimar.de
   *
   */
  public static class Factory extends Deserializer.Factory<InputStream> {
    
    // -----------------------------------------------------------------------
    // MEMBERS
    // -----------------------------------------------------------------------
    
    private final String fileSuffix;
    
    private final Supplier<String> nameSupplier;
    
    // -----------------------------------------------------------------------
    // CONSTRUCTION
    // -----------------------------------------------------------------------
    
    /**
     * Creates a new factory.
     * <p>
     * Uses an {@link EnumerationNameSupplier}.
     * </p>
     * @param fileSuffix File name suffix of target files or <code>""</code> if
     * all files should be read
     * @see #getFileSuffix()
     */
    public Factory(final String fileSuffix) {
      this(fileSuffix, new EnumerationNameSupplier());
    }
    
    /**
     * Creates a new factory.
     * @param fileSuffix File name suffix of target files or <code>""</code> if
     * all files should be read
     * @param nameSupplier The supplier that gives a name to each deserialzed
     * string
     * @see #getFileSuffix()
     */
    public Factory(
        final String fileSuffix, final Supplier<String> nameSupplier) {
      this.fileSuffix = Objects.requireNonNull(fileSuffix);
      this.nameSupplier = Objects.requireNonNull(nameSupplier);
    }
    
    // -----------------------------------------------------------------------
    // GETTERS
    // -----------------------------------------------------------------------
    
    /**
     * Gets the suffix for files that are read.
     * <p>
     * Only files with this suffix will be read, but compression suffixes are
     * stripped before this test. If this method returns the empty string, all
     * files will be read.
     * </p>
     * @return The suffix
     */
    public String getFileSuffix() {
      return this.fileSuffix;
    }

    /**
     * Gets the supplier for names.
     * <p>
     * This supplier is used to get a name for each new deserializer.
     * </p>
     * @return The supplier
     * @see #getName()
     */
    protected Supplier<String> getNameSupplier() {
      return this.nameSupplier;
    }

    /**
     * Gets the next name for the next deserializer.
     * @return The next name
     * @see #getNameSupplier()
     */
    protected String getName() {
      final Supplier<String> nameSupplier = this.getNameSupplier();
      synchronized (nameSupplier) {
        return nameSupplier.get();
      }
    }
    
    // -----------------------------------------------------------------------
    // FUNCTIONALITY
    // -----------------------------------------------------------------------

    @Override
    protected boolean isValidUncompressedFile(final String filename) {
      return filename.toLowerCase().endsWith(this.getFileSuffix());
    }

    @Override
    public Deserializer<InputStream> build(final InputStream input)
    throws IOException {
      return new InputStreamDeserializer(this.getName(), input);
    }
    
  }

}
