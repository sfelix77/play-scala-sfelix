$('#country').autocomplete({
    minLength: 2,
    source : function(request, response)
    {
        $.get("/autocomplete?country="+request.term,
        function(data, status){
            response(data)
        });
    },
    select : function(event, ui)
    {
        $('#country').val(ui.item.label )
        $('#countryForm').submit();
        return false;
    }
}).autocomplete( "instance" )._renderItem = function( ul, item ) {
    var val = $('#country').val()
    var hr = ''
    if(val.toLowerCase() == item.value.toLowerCase() || val.toLowerCase() == item.label.toLowerCase()) {
        hr = ' active'
    }
    return $( "<li>" )
        .append('<div class="list-group-item' + hr + '"><span class="badge">' + item.value + '</span> ' + item.label + '&nbsp;&nbsp;</div>')
        .appendTo( ul );
    };
