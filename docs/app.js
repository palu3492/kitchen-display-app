
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
            colorSelected: '',
            ws: undefined,
            audio: $('audio')[0],
            pingFrequency: 20000,
            errorCount: 0
        },
        computed: {
            server: function(){
                if(!this.production){
                    return 'http://localhost:5000/items'
                }
                return 'https://list-display-app.herokuapp.com/items'
            },
            wsServer: function(){
                if(!this.production){
                    return 'ws://localhost:5000/items'
                }
                return 'wss://list-display-app.herokuapp.com/items'
            }
        },
        methods: {
            webSocketSetup: function(){
                this.ws = new WebSocket(this.wsServer);
                let _this = this;
                this.ws.onopen = function (event) {
                    console.log('WebSocket connection established');
                };

                this.ws.onclose = function (event) {
                    _this.errorCount++;
                    if(_this.errorCount < 10){
                        console.log('WebSocket connection closed');
                        console.log('Attempting to establish connection');
                        _this.webSocketSetup();
                    }else{
                        console.log("Failed to connect to server")
                        alert("WebSocket crashed!");
                    }
                };

                this.ws.onerror = (e) => {
                    console.log("WebSocket error:");
                    alert("WebSocket error");
                    console.log(e)
                };

                this.ws.onmessage = (event) => {
                    _this.items = JSON.parse(event.data);
                    // this.audio.play();
                };

                setTimeout(this.pingServer, this.pingFrequency);
            },
            addItem: function(){
                let title = this.itemInput;
                if(title.length > 1){
                    let data = {title: title, color: this.colorSelected};
                    this.items.push(data); // locally add
                    this.itemInput = "";
                    let _this = this;
                    $.ajax({
                        url: _this.server,
                        type: 'PUT',
                        data: data,
                        success: result => {
                            console.log(result);
                            // _this.getItems();
                        }
                    });
                }
            },
            deleteItem: function(id){
                let _this = this;
                this.deactivateItem(id); // locally delete
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
            deactivateItem: function(id){
                this.items.forEach(item => {
                   if(item.id === id){
                       item.active = 0;
                   }
                });
            },
            // for displaying on kitchen screen (vertical orientation)
            displayMode: function(){
                this.deleteToggle = !this.deleteToggle;
                $('body').css("font-size", "3em");
                $('#options').hide();
                $('#app').css("max-width", "100vw");
                $('#app').css("min-width", "100vw");
            },
            pingServer: function(){
                // Check is WebSocket is connected
                if(this.ws.readyState === 1){
                    this.ws.send('ping');
                    console.log('ping');
                }
                setTimeout(this.pingServer, this.pingFrequency);
            }
        },
        created: function() {
            // this.getItems();
            this.webSocketSetup();
        }
    });
}
