package de.webis.readaloud;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.aitools.aq.alexa.AlexaServiceRunner;
import de.aitools.aq.alexa.Skill;
import de.aitools.aq.alexa.skill.Intent;
import de.aitools.aq.alexa.skill.OneTypeSkillLaunchResponse;
import de.aitools.aq.alexa.skill.SkillLaunchResponse;
import de.aitools.aq.alexa.skill.builtin.CancelIntent;
import de.aitools.aq.alexa.skill.builtin.HelpIntent;
import de.aitools.aq.alexa.skill.builtin.StopIntent;

/**
 * Utility class that contains the static methods that implement the
 * read aloud skill functionality.
 * 
 * <p>
 * Use {@link Builder} to create the skill object.
 * </p>
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * @author johannes.kiesel@uni-weimar.de
 *
 */
public class ReadAloudSkill {
  
  // -------------------------------------------------------------------------
  // CONSTRUCTOR
  // -------------------------------------------------------------------------
  
  private ReadAloudSkill() {}
  
  // -------------------------------------------------------------------------
  // BUILDER
  // -------------------------------------------------------------------------
  
  /**
   * Builder for {@link ReadAloudSkill}s.
   *
   * @author lukas.peter.trautner@uni-weimar.de
   * @author johannes.kiesel@uni-weimar.de
   *
   */
  public static class Builder extends Skill.Builder {

    /**
     * Creates a new builder.
     */
    public Builder() {
      this.setWebResourcesPath("web/served");
    }

    @Override
    public String getSkillName() {
      return "Read aloud!";
    }

    @Override
    protected SkillLaunchResponse buildSkillLaunch(
        final Path localizedSkillDirectoryPath,
        final Skill.Resources resources)
    throws IOException {
      return new OneTypeSkillLaunchResponse(localizedSkillDirectoryPath);
    }

    @Override
    protected List<Intent> buildIntents(
        Path localizedSkillDirectoryPath,
        final Skill.Resources resources)
    throws IOException {
      List<Intent> intents = new ArrayList<Intent>();
      intents.add(new ReadIntent(localizedSkillDirectoryPath));
      intents.add(new CancelIntent(localizedSkillDirectoryPath));
      intents.add(new StopIntent(localizedSkillDirectoryPath));
      intents.add(new HelpIntent(localizedSkillDirectoryPath));
      return intents;
    }
    
  }
  
  // -------------------------------------------------------------------------
  // MAIN
  // -------------------------------------------------------------------------
  
  /**
   * Command line interface to operate the conversational news server.
   * 
   * @param args The command line arguments for operation
   * @throws Exception If something goes wrong
   */
  public static void main(final String[] args) throws Exception {
    AlexaServiceRunner.main(new Builder(), args);
  }

}
