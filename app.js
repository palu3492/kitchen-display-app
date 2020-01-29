
let app;
window.onload = setup;

function setup(){

    Vue.component('list-item', {
        props: ['title'],
        template: '<span class="d-flex justify-content-between mb-4 item"><p>{{ title }}</p><button type="button" @click="delete">❌</button></span>'
    });

    app = new Vue({
        el: '#app',
        data: {
            items: [
                {title: 'Bakery and Bread'},
                {title: 'Meat and Seafood'},
                {title: 'Pasta and Rice'},
                {title: 'Oils, Sauces, Salad Dressings, and Condiments'},
                {title: 'Cereals and Breakfast Foods'}
            ],
            itemInput: ""
        },
        methods: {
            addItem: function(){
                let title = this.itemInput;
                let _this = this;
                $.ajax({
                    url: 'http://localhost:5000/new-item',
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
                $.getJSON('http://localhost:5000/items', data => {
                    _this.items = data;
                });
            },
            delete: function(){
                console.log('delete');
            }
        },
        created: function() {
            this.getItems();
        }
    });
}

