export default function ItemRow(item,i) {


    return(<>
        
        <tr>
            <td>{item.id}</td>
            <td>{item.name}</td>
            <td>{item.stock}</td>
            <td>{item.capacity}</td>
            <td><input key={i}/></td>
        </tr>
        
    </>);
}
