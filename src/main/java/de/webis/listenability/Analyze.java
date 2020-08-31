package de.webis.listenability;

public class Analyze extends de.aitools.commons.uima.Analyze {

  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------

  /**
   * Default configuration file.
   */
  public static final String DEFAULT_CONFIGURATION =
      "listenability-tools.conf";

  // -------------------------------------------------------------------------
  // CONSTRUCTION
  // -------------------------------------------------------------------------

  /**
   * Creates the command line interface without a description.
   * @see #Analyze(String)
   */
  public Analyze() {
    super("Runs the listenability analysis engine.");
  }

  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------
  
  @Override
  protected String[] getDefaultConfigNames() {
    return new String[] {
        de.aitools.commons.uima.Analyze.DEFAULT_CONFIGURATION,
        DEFAULT_CONFIGURATION
    };
  }

  /**
   * Runs the analysis engine according to default and optional given
   * configuration.
   * <p>
   * Use <code>--help</code> for more information.
   * </p>
   * @param args Command line arguments
   * @throws Exception If something goes wrong
   */
  public static void main(final String[] args) throws Exception {
    final Analyze cli = new Analyze();
    cli.run(args);
  }

}
