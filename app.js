
let app;
window.onload = setup;

function setup(){

    Vue.component('list-item', {
        props: ['title', 'id'],
        template: `<span class="d-flex justify-content-between mb-2 item">
                        <p>{{ title }}</p>
                        <button type="button" v-on:click="$emit('delete-item', id)">❌</button>
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
            production: true
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
            }
        },
        created: function() {
            this.getItems();
        }
    });

    setTimeout(reload, 60000);
}

function reload(){
    location.reload();
    console.log('reload');
}
