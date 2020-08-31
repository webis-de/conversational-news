class Suggestion {
  constructor(uimaDocument, suggestion) {
    this.text = suggestion.getAttribute("text");
    if (suggestion.hasAttribute("scores")) {
      this.scores = uimaDocument
        .getFSArrayElements(suggestion.getAttribute("scores"))
        .map(score => new Score(uimaDocument, score));
    } else {
      this.scores = [];
    }
  }
}

class Explanation {
  constructor(uimaDocument, explanation) {
    this.key = explanation.getAttribute("key");
    if (explanation.hasAttribute("value")) {
      this.value = explanation.getAttribute("value");
    } else {
      this.value = null;
    }
    if (explanation.hasAttribute("reference")) {
      this.reference = uimaDocument.queryId(explanation.getAttribute("reference"));
    } else {
      this.reference = null;
    }
  }
}

class Score {
  constructor(uimaDocument, score) {
    this.name = score.getAttribute("name");
    this.value = score.getAttribute("value");
    if (score.hasAttribute("explanations")) {
      this.explanations = uimaDocument
        .getFSArrayElements(score.getAttribute("explanations"))
        .map(explanation => new Explanation(uimaDocument, explanation));
    } else {
      this.explanations = [];
    }
  }
}

class ScoredUnit {
  constructor(uimaDocument, scoredUnit) {
    this.begin = parseInt(scoredUnit.getAttribute("begin"));
    this.end = parseInt(scoredUnit.getAttribute("end"));
    this.unit = uimaDocument.queryId(scoredUnit.getAttribute("unit"));
    if (scoredUnit.hasAttribute("scores")) {
      this.scores = uimaDocument
        .getFSArrayElements(scoredUnit.getAttribute("scores"))
        .map(score => new Score(uimaDocument, score));
    } else {
      this.scores = [];
    }
    if (scoredUnit.hasAttribute("suggestions")) {
      this.suggestions = uimaDocument
        .getFSArrayElements(scoredUnit.getAttribute("suggestions"))
        .map(suggestion => new Suggestion(uimaDocument, suggestion));
    } else {
      this.suggestions = [];
    }
  }
}

function getScoredUnits(uimaDocument) {
  return uimaDocument
    .querySelectorAll("ScoredUnit")
    .map(scoredUnit => new ScoredUnit(uimaDocument, scoredUnit));
}
