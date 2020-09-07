const bodyEditor = new Quill('#bodyEditor', {
  modules: { toolbar: '#bodyToolbar' },
  theme: 'snow'
});


const scoredType = new QuillPopperType("scored", (popper, data) => {
  const currentScores = {};
  if (data.scores) {
    for (let s = 0; s <= data.scores.length; ++s) {
      const score = data.scores[s];
      currentScores[score.name] = score.value;

      const scoreElement = document.createElement("div");
      scoreElement.classList.add("card-scoreElement", "score");
      scoreElement.setAttribute("data-score-name", score.name);
      scoreElement.innerText = score.name;
      popper.appendChild(scoreElement);
    }
  }

  if (data.suggestions && data.suggestions.length > 0) {
    const suggestionsElement = document.createElement("ul");
    suggestionsElement.classList.add("list-group", "list-group-flush", "suggestions");

    for (let s = 0; s <= data.suggestions; ++s) {
      const suggestion = data.suggestions[s];

      const suggestionElement = document.createElement("li");
      suggestionElement.classList.add("list-group-item", "suggestion");
      suggestionElement.innerText = suggestion.text;
      suggestionsElement.appendChild(suggestionElement);

      // TODO: data-improves="['score-name', 'score-name', ...]"
    }

    popper.appendChild(suggestionsElement);
  }
});
scoredType.initialize(bodyEditor);
