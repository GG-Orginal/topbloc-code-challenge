export default function ItemRow(sku) {


    return(<>
        
        <tr>
            <td>{sku.id}</td>
            <td>{sku.name}</td>
            <td>{sku.stock}</td>
            <td>{sku.capacity}</td>
            <td>Order Amount</td>
        </tr>
        
    </>);
}
