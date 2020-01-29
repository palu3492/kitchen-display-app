
let app;
window.onload = setup;

function setup(){

    Vue.component('list-item', {
        props: ['title'],
        template: '<span class="d-flex justify-content-between mb-4 item"><p>{{ title }}</p></span>'
        // <button type="button" @click="deleteItem">❌</button>
    });

    app = new Vue({
        el: '#app',
        data: {
            items: [
                {id: 0, title: 'Bakery and Bread'},
                {id: 1, title: 'Meat and Seafood'}
            ],
            itemInput: "",
            server: 'https://list-display-app.herokuapp.com/items'
        },
        methods: {
            addItem: function(){
                let title = this.itemInput;
                let _this = this;
                $.ajax({
                    url: _this.server,
                    type: 'PUT',
                    data: {title: title},
                    success: result => {
                        _this.getItems();
                    }
                });
            },
            getItems: function(){
                let _this = this;
                // Fetch array of list items from API
                $.getJSON(_this.server, data => {
                    _this.items = data;
                    console.log(_this.items);
                });
            },
            deleteItem: function(id){
                let _this = this;
                $.ajax({
                    url: _this.server,
                    type: 'DELETE',
                    data: {id: id},
                    success: result => {
                        _this.getItems();
                    }
                });
            }
        },
        created: function() {
            this.getItems();
        }
    });
}

