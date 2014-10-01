var guestMessage = "A guest account allows a user to explore the functionality of Chembench using publicly "
        + "available datasets, predictions on single molecules, and modeling using Random Forests. "
        + "<br><br> All guest data is deleted when you leave the site or become inactive for 90 minutes. "
        + "For additional functionality, please register an account.";

$(document).ready(function() {
    $(".nav-list li").mouseup(function(event) {
        if (event.which === 1) {
            window.location = $(this).find("a").attr("href");
        }
    });

    $("#nav-button-home").addClass("active");

    $("#guest-login").click(function(event) {
        event.preventDefault();
        bootbox.alert(guestMessage, function() {
            window.location = $("#guest-login").attr("href");
        });
    });

    $(".logout-button").click(function() {
        window.location = "logout";
    });
});