package de.webis.readaloud;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.ask.model.IntentConfirmationStatus;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response.Builder;

import de.aitools.aq.alexa.skill.GenericIntent;
import de.aitools.aq.alexa.skill.SlotValue;
import de.aitools.aq.alexa.skill.User;
import de.aitools.aq.alexa.speech.SsmlValidator;
import de.aitools.aq.alexa.speech.SsmlValidator.ValidationReport;

/**
 * Intent that reads the text that is currently selected in the browser.
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * @author johannes.kiesel@uni-weimar.de
 *
 */
public class ReadIntent extends GenericIntent {

  // -------------------------------------------------------------------------
  // LOGGING
  // -------------------------------------------------------------------------

  private static Logger LOG = LoggerFactory.getLogger(ReadIntent.class);
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * Name of the intent in the file system and the model JSON.
   */
  private static final String NAME = "read";
  
  /**
   * Responses for successfully reading a users's text.
   */
  private static final String RESPONSE_TYPE_SUCCESS = "success";
  
  /**
   * Slot for the users's text.
   */
  private static final String RESPONSE_SLOT_USER_TEXT = "userText";
  
  /**
   * Responses for when the user is not linked with alexa2web.
   */
  private static final String RESPONSE_TYPE_ERROR_NOT_LINKED = "errorNotLinked";
  
  /**
   * Responses for when the user's text could not be found.
   */
  private static final String RESPONSE_TYPE_ERROR_NO_TEXT = "errorNoText";
  
  /**
   * Responses for when the user's text is not a valid SSML fragment.
   */
  private static final String RESPONSE_TYPE_ERROR_INVALID_TEXT =
      "errorInvalidText";
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------

  private final SsmlValidator validator;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------
  
  /**
   * Creates a new intent to read a user's text.
   * 
   * @param localizedSkillDirectoryPath The directory of the localized skill
   * of which the intent should be a part of
   * @throws IllegalArgumentException If a speech file exists but is empty
   * @throws NoSuchElementException If a speech sample contains an unknown slot
   * @throws IOException If the response file for a response type does
   * not exist
   */
  public ReadIntent(final Path localizedSkillDirectory)
  throws FileNotFoundException, IllegalArgumentException,
  IOException {
    super(new Configuration(NAME, localizedSkillDirectory)
        .addResponseType(RESPONSE_TYPE_SUCCESS, RESPONSE_SLOT_USER_TEXT)
        .addResponseType(RESPONSE_TYPE_ERROR_NOT_LINKED)
        .addResponseType(RESPONSE_TYPE_ERROR_NO_TEXT));
    this.validator = new SsmlValidator();
  }

  // -------------------------------------------------------------------------
  // GETTER
  // -------------------------------------------------------------------------

  /**
   * Gets the SSML validator.
   * @return The validator
   */
  protected SsmlValidator getValidator() {
    return this.validator;
  }

  // -------------------------------------------------------------------------
  // FUNCTIONALITY: REQUEST HANDLING
  // -------------------------------------------------------------------------

  @Override
  protected Builder onRequest(
      final IntentConfirmationStatus intentConfirmationStatus,
      final Map<String, SlotValue> slotValues,
      final RequestEnvelope status,
      final User user) {
    if (!user.checkIfLinkedWithAlexa2Web()) {
      return this.respond(RESPONSE_TYPE_ERROR_NOT_LINKED, slotValues)
          .withShouldEndSession(true);
    }

    final String userText = user.getBrowser().getSelectionNow();
    LOG.debug("Got selection from user {}: {}", user.getId(), userText);
    if (userText == null || userText.isBlank()) {
      return this.respond(RESPONSE_TYPE_ERROR_NO_TEXT, slotValues)
          .withShouldEndSession(true);
    }

    final ValidationReport validationReport =
        this.getValidator().validateFragment(userText);
    LOG.debug("Validated selection of user {} with errors={}",
        user.getId(), validationReport.containsErrors());
    for (final String warning : validationReport.getWarnings()) {
      LOG.debug("WARN for user {}: {}", user.getId(), warning);
    }
    for (final String error : validationReport.getErrors()) {
      LOG.debug("ERR  for user {}: {}", user.getId(), error);
    }
    user.getBrowser().send(validationReport);
    LOG.debug("Send validation report back to user {}", user.getId());
    if (validationReport.containsErrors()) {
      return this.respond(RESPONSE_TYPE_ERROR_INVALID_TEXT, slotValues)
          .withShouldEndSession(true);
    } else {
      final Map<String, String> responseSlotValues =
          Map.of(RESPONSE_SLOT_USER_TEXT, userText);
      return this.respond(
            RESPONSE_TYPE_SUCCESS, slotValues, responseSlotValues)
          .withShouldEndSession(true);
    }
  }
  
}
