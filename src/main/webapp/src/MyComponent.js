/*import React from 'react';
import axios from 'axios'

class MyComponent extends React.Component {
  componentDidMount() {
    const apiUrl = 'http://localhost:8080/dashboard/modelTraffic';
    axios.get(apiUrl).then((repos) => {
          const allRepos = repos.data;
          setAppState({ loading: false, repos: allRepos });
          console.log('This is your data', allRepos);
    });
    setAppState({ loading: true });
    const apiUrl = 'https://api.github.com/users/hacktivist123/repos';
    axios.get(apiUrl).then((repos) => {
      const allRepos = repos.data;
      setAppState({ loading: false, repos: allRepos });
    });
  }
  render() {
    return <h1>my Component has Mounted, Check the browser 'console' </h1>;
  }
  useEffect()
  {
      setAppState({ loading: true });
      const apiUrl = 'https://api.github.com/users/hacktivist123/repos';
      axios.get(apiUrl).then((repos) => {
        const allRepos = repos.data;
        setAppState({ loading: false, repos: allRepos });
      });
   }
}
export default MyComponent;*/