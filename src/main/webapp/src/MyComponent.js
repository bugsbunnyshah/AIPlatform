import React from 'react';
import axios from 'axios'

class MyComponent extends React.Component {
  componentDidMount() {
    //const apiUrl = 'https://api.github.com/users/hacktivist123/repos';
    /*const apiUrl = 'http://localhost:8080/microservice';
    fetch(apiUrl)
      .then((response) => response.text())
      .then((data) => console.log('This is your data', data));*/
    //const apiUrl = 'https://api.github.com/users/hacktivist123/repos';
    const apiUrl = 'http://localhost:8080/dashboard/modelTraffic';
    axios.get(apiUrl).then((repos) => {
          const allRepos = repos.data;
          //setAppState({ loading: false, repos: allRepos });
          console.log('This is your data', allRepos);
    });
  }
  render() {
    return <h1>my Component has Mounted, Check the browser 'console' </h1>;
  }
}
export default MyComponent;