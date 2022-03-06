import React, { Component } from 'react'
import ItemRow from './ItemRow';

class Challenge2 extends React.Component {

    constructor(props){
        super();
        this.state = {
            items: [],
            restock: 0,
        }
    }

    getLowStockAPI = () => {
        fetch('http://localhost:4567/low-stock')
        .then(response => response.json())
        .then(data => {
            // console.log(data);
            this.state.items = data;
            this.setState({})
        })
    }

    getCheapestRestockAPI = () => {
        console.log(this.state.items);

        for (let j = 0; j < document.getElementsByTagName("INPUT").length; j++){
            this.state.items[j].quantity = Number(document.getElementsByTagName("INPUT")[j].value);
        }

        fetch('http://localhost:4567/restock-cost', {
            method: 'post',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({
                 "items": this.state.items,
            })
         })
        .then(response => response.json())
        .then(data => {
            console.log(data);
            this.state.restock = data;
            this.setState({})
        }
        );
    }

    renderTableRows = () => {
        return this.state.items.map((item,i) => {
          return (
            ItemRow(item,i)
          )
        })
      }

      renderRestock = () => {
        return this.state.restock
      }

  render () {
  return (
    <>
      <table>
        <thead>
          <tr>
            <td>SKU</td>
            <td>Item Name</td>
            <td>Amount in Stock</td>
            <td>Capacity</td>
            <td>Order Amount</td>
          </tr>
        </thead>
        <tbody>
        {this.renderTableRows()}
        </tbody>
      </table>

      <div>Total Cost: {this.renderRestock()}</div>

      <button id="getLowStockButton" onClick={this.getLowStockAPI}>Get Low-Stock Items</button>
      <button id="getReorderCostButton" onClick={this.getCheapestRestockAPI}>Determine Re-Order Cost</button>

    </>
  );
    }
}
export default Challenge2;