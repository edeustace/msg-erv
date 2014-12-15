$(document).ready(function(){

    $('.btn-primary').click(function(){

      var recipient = $('#recipient').val().trim();

      var data = {
        msg: $('#msg').val().trim()
      };

      $.ajax({
        type: "PUT",
        url: '/editor/message/' + recipient,
        data: JSON.stringify(data),
        contentType:'application/json'
      }).done(function( msg ) {
          console.log( "Data Saved: " + msg );
      });
    });
  });