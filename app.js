
let app;
window.onload = setup;

function setup(){

    Vue.component('list-item', {
        props: ['title', 'id', 'deletetoggle', 'color'],
        template: `<span class="d-flex justify-content-between item pl-3">
                        <p>{{ title }}</p>
                        <div class="d-flex">
                            <button type="button" class="delete-button" v-if="deletetoggle" v-on:click="$emit('delete-item', id)">❌</button>
                            <div class="color ml-2" :style="'background-color: '+color"></div>
                        </div>
                    </span>`
    });

    app = new Vue({
        el: '#app',
        data: {
            items: [
                {id: 10, title: 'Lime sparkling water', color: 'red'},
                {id: 11, title: 'Cream cheese', color: 'green'}
            ],
            itemInput: "",
            production: true,
            deleteToggle: true,
            colorSelected: ''
        },
        computed: {
            server: function(){
                if(!this.production){
                    return 'http://localhost:5000/items'
                }
                return 'https://list-display-app.herokuapp.com/items'
            }
        },
        methods: {
            webSocketSetup: function(){
                let ws = new WebSocket('wss://list-display-app.herokuapp.com/items');

                ws.onopen = function (event) {
                    ws.send("Give me items please.");
                };

                let _this = this;
                ws.onmessage = (event) => {
                    _this.items = JSON.parse(event.data);
                };

                // ws.onopen = function (event) {
                //     // exampleSocket.send("Here's some text that the server is urgently awaiting!");
                //     console.log('open');
                // };


                // let _this = this;
                // // Fetch array of list items from API
                // $.getJSON(_this.server, data => {
                //     let count = _this.items.length;
                //     _this.items = data;
                //     console.log('received items');
                //     if(count > 0 && count < data.length){
                //         playSound();
                //     }
                // });
            },
            addItem: function(){
                let title = this.itemInput;
                if(title.length > 1){
                    let color = this.colorSelected;
                    this.itemInput = "";
                    let _this = this;
                    $.ajax({
                        url: _this.server,
                        type: 'PUT',
                        data: {title: title, color: color},
                        success: result => {
                            console.log(result);
                            // _this.getItems();
                        }
                    });
                }
            },
            deleteItem: function(id){
                let _this = this;
                $.ajax({
                    url: _this.server,
                    type: 'DELETE',
                    data: {id: id},
                    success: result => {
                        console.log(result);
                        // _this.getItems();
                    }
                });
            },
            displayMode: function(){
                this.deleteToggle = !this.deleteToggle;
                $('body').css("font-size", "3em");
                $('#options').hide();
                $('#app').css("max-width", "100vw");
                $('#app').css("min-width", "100vw");
            }
        },
        created: function() {
            // this.getItems();
            this.webSocketSetup();
        }
    });

    // setTimeout(refreshList, 60000);
}

// function refreshList(){
//     app.getItems();
//     setTimeout(refreshList, 60000);
// }

function playSound(){
    $('audio')[0].play();
}
