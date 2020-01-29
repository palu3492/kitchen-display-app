
let app;
window.onload = setup;

function setup(){

    Vue.component('list-item', {
        props: ['title', 'id', 'deletetoggle'],
        template: `<span class="d-flex justify-content-between item px-3">
                        <p>{{ title }}</p>
                        <button type="button" v-if="deletetoggle" v-on:click="$emit('delete-item', id)">❌</button>
                    </span>`
    });

    app = new Vue({
        el: '#app',
        data: {
            items: [
                {id: 0, title: 'Bakery and Bread'},
                {id: 1, title: 'Meat and Seafood'}
            ],
            itemInput: "",
            production: true,
            deleteToggle: true
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
            getItems: function(){
                let _this = this;
                // Fetch array of list items from API
                $.getJSON(_this.server, data => {
                    _this.items = data;
                });
            },
            addItem: function(){
                let title = this.itemInput;
                this.itemInput = "";
                let _this = this;
                $.ajax({
                    url: _this.server,
                    type: 'PUT',
                    data: {title: title},
                    success: result => {
                        console.log(result);
                        _this.getItems();
                    }
                });
            },
            deleteItem: function(id){
                let _this = this;
                $.ajax({
                    url: _this.server,
                    type: 'DELETE',
                    data: {id: id},
                    success: result => {
                        console.log(result);
                        _this.getItems();
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
            this.getItems();
        }
    });

    setTimeout(refreshList, 60000);
}

function refreshList(){
    app.getItems();
    console.log('reload');
}
