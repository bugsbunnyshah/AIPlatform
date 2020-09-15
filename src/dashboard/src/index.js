import React from 'react';
import ReactDOM from 'react-dom';
import Dashboard from './Dashboard';
import MyComponent from './MyComponent';



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
  <MyComponent />,
  document.querySelector('#root'),
);
