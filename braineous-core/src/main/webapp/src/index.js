import React from 'react';
import ReactDOM from 'react-dom';
//import Dashboard from './Dashboard';
import App from './App';



/*function tick() {
  const element = (
  fetch(' http://localhost:8080/dashboard/modelTraffic')
          .then(res => res.json())
          .then((data) => {
            //this.setState({ contacts: data })
            console.log(data);
          })
          .catch(console.log)
  );
  ReactDOM.render(
    element,
    document.getElementById('root')
  );
}*/

ReactDOM.render(
  <App />,
  document.querySelector('#root'),
);
