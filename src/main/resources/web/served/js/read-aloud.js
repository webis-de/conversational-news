class ReadAloudClient {

  /**
   * Creates a new client.
   * @param alexa2WebClient The Alexa2Web client to use for communication
   */
  constructor(alexa2WebClient) {
    this.ssml = "";
    this.alexa2WebClient = alexa2WebClient;
    if (!this.alexa2WebClient.isConnected() && !this.alexa2WebClient.isConnecting()) {
      this.alexa2WebClient.connect();
    }
    this.alexa2WebClient.on("state", state => {
      if (state === "listening") {
        window.setTimeout(() => {
          if (this.alexa2WebClient.state === "listening") {
            this._speakRead();
          }
        }, 100);
      }
    });
  }

  /**
   * Creates a new client
   */
  static create() {
    const readAloudClient = new ReadAloudClient(new Alexa2WebClient({
      endpoint: ReadAloudClient.defaultAlexa2WebEndpoint,
      skillId: ReadAloudClient.defaultSkillId,
      skillClient: new Alexa2WebSkillClient({
        endpoint: ReadAloudClient.defaultSkillEndpoint
      })
    }));
    readAloudClient.alexa2WebClient.skillClient.getSelectionFunction =
      () => readAloudClient.getSsmlToRead();
    return readAloudClient;
  }

  _speakRead() {
    const _this = this;
    const audio = new Audio("audio/read.mp3");
    audio.muted = true;
    audio.play().then(async function() {
      const stream = audio.captureStream();
      const track = stream.getAudioTracks()[0];
      _this.alexa2WebClient.pc.getSenders()[0].replaceTrack(track); 
    });
    audio.onended = () => {
      _this.alexa2WebClient.pc.getSenders()[0].replaceTrack(_this.alexa2WebClient.track); 
    }
  }

  getSsmlToRead() {
    return this.ssml;
  }

  read(ssml) {
    this.ssml = ssml;
    this.alexa2WebClient.talk();
  }

}

ReadAloudClient.defaultAlexa2WebEndpoint = "wss://alexa2web.webis.de/rtc";
ReadAloudClient.defaultSkillId = "amzn1.ask.skill.e5ca39f4-e30c-40c0-9359-fde12d85c2f2";
ReadAloudClient.defaultSkillEndpoint = "wss://readaloud.webis.de/alexa2web/";
