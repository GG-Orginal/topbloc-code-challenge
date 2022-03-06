import Instructions from "../components/Instructions";
import Challenge from "../components/Challenge";
import Challenge2 from "../components/Challenge2";
import "../styles.css";
import ItemRow from "../components/ItemRow";

export default function App() {
  return (
    <>
      <Instructions />
      <div className="challenge-container">
        <Challenge2 />
      </div>
    </>
  );
}
