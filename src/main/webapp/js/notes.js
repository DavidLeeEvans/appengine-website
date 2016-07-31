var initPage = function() {
  $.getJSON('/api/notes', function(data) {
    $('#notes-downloads').text("Total downloads: " + data["downloads"]);
    var ul = $('<ul>').appendTo('#notes-list');
    $.each(data["notes"], function(i, item) {
      var link = $(document.createElement('a'))
            .attr('href', item["download_url"])
            .text(item["name"] + " - " + item["downloads"]);
        ul.append(
            $(document.createElement('li')).append(link));
    });
  });
}
