// REGISTER BLOTS
{
  const BlockEmbed = Quill.import("blots/block/embed");
  const Inline = Quill.import("blots/inline");

  // AUDIO
  class AudioBlot extends BlockEmbed {
    static create(url) {
      const node = super.create();
      node.setAttribute("src", url);
      node.setAttribute("controls", "");
      return node;
    }
    static value(node) {
      return node.getAttribute("src");
    }
  }
  AudioBlot.blotName = "audio";
  AudioBlot.tagName = "audio";
  Quill.register(AudioBlot);

  // LANGUAGE
  class LangBlot extends Inline {
    static create(language){
      const node = super.create(); 
      node.setAttribute("lang", language);
      return node;
    }
    static formats(domNode) {
      return domNode.getAttribute("lang");
    }
    format(name, value) {
      if (name == LangBlot.blotName && value) {
        this.domNode.setAttribute("lang", value);
      } else {
        return super.format(name, value);
      }
    }
  }
  LangBlot.blotName = 'lang';
  LangBlot.tagName = 'lang';
  Quill.register(LangBlot);

  // PHONEME
  class PhonemeBlot extends Inline {
    static create(phoneme){
      const node = super.create(); 
      node.setAttribute("phoneme", phoneme);
      return node;
    }
    static formats(domNode) {
      return domNode.getAttribute("phoneme");
    }
    format(name, value) {
      if (name == PhonemeBlot.blotName && value) {
        this.domNode.setAttribute("phoneme", value);
      } else {
        return super.format(name, value);
      }
    }
  }
  PhonemeBlot.blotName = 'phoneme';
  PhonemeBlot.tagName = 'phoneme';
  Quill.register(PhonemeBlot);

  // VOICE
  class VoiceBlot extends Inline {
    static create(name){
      const node = super.create(); 
      node.setAttribute("name", name);
      return node;
    }
    static formats(domNode) {
      return domNode.getAttribute("name");
    }
    format(name, value) {
      if (name == VoiceBlot.blotName && value) {
        this.domNode.setAttribute("name", value);
      } else {
        return super.format(name, value);
      }
    }
  }
  VoiceBlot.blotName = 'voice';
  VoiceBlot.tagName = 'voice';
  Quill.register(VoiceBlot);
}

// REGISTER ICONS
{
  const icons = Quill.import("ui/icons");
  icons["audio"] = "<i class='fa fa-volume-up' title='Insert an audio clip by its URL'></i>";
  icons["phoneme"] = "<i class='fab fa-creative-commons-sampling' title='Provide the pronounciation for the selected text in IPA format'></i>";
  icons["read"] = "<i class='far fa-comment-dots' title='Read the whole text or the selection'></i>";
}

function makeReadAloudQuillToolbar(readAloudClient) {

  // HELPER FUNCTIONS

  const escapeForSsml = text => {
    const element = document.createElement('div');
    element.innerText = text;
    return element.innerHTML
      .replace(/(<br>)*$/g, "")
      .replace(/<br>/g, "<break strength='x-strong'/>");
  }

  const getQuillSsmlSelection = quill => {
    const selection = getQuillSelection(quill);
    let ssmlFragment = "";
    selection.ops.forEach(operation => {
      // opening tags
      let closingTags = "";
      if (operation.attributes) {
        if (operation.attributes.header) {
          ssmlFragment += "<break time='1s'/>";
        } else if (operation.attributes.bold) {
          ssmlFragment += " , <emphasis>";
          closingTags = "</emphasis>" + closingTags;
        } else if (operation.attributes.italic) {
          ssmlFragment += "<emphasis>";
          closingTags = "</emphasis>" + closingTags;
        }
        if (operation.attributes.lang) {
          ssmlFragment += "<lang xml:lang='" + operation.attributes.lang + "'>";
          closingTags = "</lang>" + closingTags;
        }
        if (operation.attributes.voice) {
          ssmlFragment += "<voice name='";
          if (operation.attributes.voice === "male") {
            ssmlFragment += "Brian";
          } else {
            console.log("Unknown voice: " + operation.attributes.voice);
            ssmlFragment += "Alexa";
          }
          ssmlFragment += "'>";
          closingTags = "</voice>" + closingTags;
        }
        if (operation.attributes.phoneme) {
          ssmlFragment += "<phoneme alphabet='ipa' ph='" + operation.attributes.phoneme + "'>";
          closingTags = "</phoneme>" + closingTags;
        }
      }

      // string content or empty tags
      if (typeof(operation.insert) === "string") {
        ssmlFragment += escapeForSsml(operation.insert);
      } else {
        if (operation.insert.audio) {
          ssmlFragment += "<audio src='";
          ssmlFragment += operation.insert.audio
            .replace(/https:\/\/d3qhmae9zx9eb.cloudfront.net(.*).mp3$/, "soundbank://soundlibrary$1");
          ssmlFragment += "'/>";
        }
      }

      // closing tags
      ssmlFragment = ssmlFragment + closingTags;
    });
    return ssmlFragment;
  }

  const getQuillSelection = quill => {
    const range = quill.getSelection();
    if (range === null || range.length === 0) {
      return quill.getContents()
    } else {
      return quill.getContents(range);
    }
  }

  // TOOLBAR HANDLERS

  const audioHandler = function() {
    const url = prompt("Enter the URL of the audio file that you want to embed:")
      .replace(/soundbank:\/\/soundlibrary(.*)/, "https://d3qhmae9zx9eb.cloudfront.net$1.mp3");
    const range = this.quill.getSelection();
    this.quill.insertEmbed(range.index, 'audio', url);
  }

  const phonemeHandler = function(value) {
    const selection = this.quill.getSelection();
    if (selection !== null && selection.length > 0) {
      const pronounciation = prompt("Enter the IPA description for '" + this.quill.getText(selection) + "'");
      this.quill.format("phoneme", pronounciation);
    } else {
      alert("You must first select the text for which you want to specify the pronounciation");
    }
  }

  const readHandler = function() {
    readAloudClient.read(getQuillSsmlSelection(this.quill));
  }

  // ANIMATE READ ICON

  readAloudClient.alexa2WebClient.on("state", state => {
    const readButtons = document.querySelectorAll(".ql-read");
    if (state === "listening" || state === "invoking" || state === "thinking" || state === "speaking" || state === "expecting") {
      readButtons.forEach(button => {
        if (!button.hasAttribute("disabled")) {
          button.setAttribute("disabled", "true");
          const svg = button.querySelector("svg");
          svg.classList.add("fa-sync", "fa-spin", "fa");
          svg.classList.remove("fa-comment-dots", "far");      
        }
      });
    } else {
      readButtons.forEach(button => {
        if (button.hasAttribute("disabled")) {
          button.removeAttribute("disabled");
          const svg = button.querySelector("svg");
          svg.classList.add("fa-comment-dots", "far");    
          svg.classList.remove("fa-sync", "fa-spin", "fa");
        }
      });
    }
  });

  // RETURN

  return {
    container: [
      [
        { "header": 1 },
        { "header": 2 }
      ], [
        "bold",
        "italic",
        "phoneme",
        { "lang": [false, "fr-FR", "de-DE"] },
        { "voice": [false, "male"] },
        "clean"
      ],
      ["audio"],
      ["read"]
    ],
    handlers: {
      "audio": audioHandler,
      "phoneme": phonemeHandler,
      "read": readHandler
    }
  };
}


