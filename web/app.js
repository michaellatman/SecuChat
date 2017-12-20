$(document).ready(function () {    
    var ivs;
    var key;
    var myDataRef = new Firebase('https://secuchat.firebaseio.com/message/-JYlLp58_EHkwMqMGDxI');
    myDataRef.on('child_added', function (snapshot) {
        var message = snapshot.child("text").val();
        var cipherParams = CryptoJS.lib.CipherParams.create({
            ciphertext: CryptoJS.enc.Base64.parse(message),
            salt:CryptoJS.enc.Hex.parse(snapshot.child("salt").val())
          });
          var decrypted = CryptoJS.AES.decrypt(
              cipherParams,
              "Secret Passphrase", {iv:CryptoJS.enc.Hex.parse(snapshot.child("iv").val())});

        displayChatMessage(decrypted.toString(CryptoJS.enc.Utf8));
    });

    function displayChatMessage(message) {
        $('<div/>').text(message).prepend($('<em/>').text("Anon" + ': ')).appendTo($('#displayChat'));
        // make text scroll everytime someone posts
        window.scrollTo(0,document.body.scrollHeight);

    };

    $('#enterText').keypress(function(e){
        if(e.which == 13){
            //Send message
            var text = $(this).val();
            var encrypted = CryptoJS.AES.encrypt(text, "Secret Passphrase");
            ivs = encrypted.iv;
            key = encrypted.key;
            var ciphertext = encrypted.ciphertext.toString(CryptoJS.enc.Base64);
            $(this).val("");
            myDataRef.push().update({text:ciphertext,iv:ivs.toString(),salt:encrypted.salt.toString()});

        }
    });
});